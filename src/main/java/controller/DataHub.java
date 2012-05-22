package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.ArrayUtils;

import util.Operator;
import db.Database;
import db.DatabaseAccessException;

/**
 * The class {@code DataHub} is used to request data from the {@link Database}.
 */
public class DataHub extends Observable implements Observer {

	/**
	 * The {@link Database}, where all elements are stored.
	 */
	private final Database database;

	/**
	 * The {@link GroupController}, where to get a list of {@link Group}s.
	 */
	private final GroupController groupController;

	/**
	 * The {@link SubspaceController}, where to get the currently active {@link Subspace}.
	 */
	private final SubspaceController subspaceController;

	/**
	 * Holds intersections on constraints, for each group.
	 */
	private HashMap<Integer, HashSet<Integer>> uniqGroupIds = null;

	/**
	 * Holds all groups from the groupcontroller.
	 */
	private Group[] allGroups = null;

	/**
	 * Union on all active/visible groups.
	 */
	private HashSet<Integer> uniqIds = null;

	/**
	 * Holds all ids from constraints and is read on a range basis by the worker threads.
	 */
	private int[] uniqWorkerArray = null;

	/**
	 * Threadpool to speed up the getData() bottleneck.
	 */
	private ExecutorService xServ = null;

	/**
	 * Number of Threads in the threadpool.
	 */
	private int numberOfThreads = 0;

	/**
	 * Caches elements, to optimize performance of queries with same subspace/constraints.
	 */
	private ElementData[] elementCache = null;

	/**
	 * Constructor for a new {@code DataHub}. All parameters may not be {@code null}.
	 *
	 * @param database
	 *            the {@link Database}, where the elements are stored.
	 * @param groupController
	 *            the {@link GroupController}, where to get all active {@link Group}s.
	 * @param subspaceController
	 *            the {@link SubspaceController}, where to get the active {@link Subspace}.
	 */
	public DataHub(Database database, GroupController groupController, SubspaceController subspaceController) {
		if (database == null || groupController == null || subspaceController == null) {
			throw new IllegalArgumentException("database or one controller is null");
		}
		this.database = database;

		// we need to update, if groups changes
		this.groupController = groupController;
		this.groupController.addObserver(this);

		// we need to update, if subspace changes
		this.subspaceController = subspaceController;
		this.subspaceController.addObserver(this);

		int maxIds;
		int maxGroups;

		try {
			maxIds = database.getConnection().createStatement().executeQuery("SELECT COUNT(Id) FROM Objects;")
					.getInt(1);
		} catch (SQLException e1) {
			maxIds = -1;
		}

		try {
			maxGroups = this.groupController.getGroups().length;
		} catch (DatabaseAccessException e) {
			maxGroups = -1;
		}

		if (maxIds != -1 && maxGroups != -1) {
			// boost performance by specifying capacity and load factor
			this.uniqGroupIds = new HashMap<Integer, HashSet<Integer>>((int) (maxGroups * 1.2f), 1.f);
			this.uniqIds = new HashSet<Integer>((int) (maxIds * 1.2f), 1.f);
		} else {
			// no performance boost without load factor and capacity specification
			this.uniqGroupIds = new HashMap<Integer, HashSet<Integer>>();
			this.uniqIds = new HashSet<Integer>();
		}

		// initialize threadpool; assume pool size ~ proc+2
		this.numberOfThreads = (Runtime.getRuntime().availableProcessors()) + 2;
		this.xServ = Executors.newFixedThreadPool(this.numberOfThreads);
	}

	/**
	 * Filters selected ids based on active constraints
	 *
	 * @throws DatabaseAccessException
	 *                                  if read operation failed in {@link Database}.
	 */
	private void evaluateConstraints() throws DatabaseAccessException {
		for (Group group : this.allGroups) {
			// select constraints accordingly
			if (group.isVisible() && group.getConstraints().length > 0) {
				Constraint[] allConstraints = group.getConstraints();

				// holds sets for each static constraint
				ArrayList<HashSet<Integer>> staticConstraintSets = new ArrayList<HashSet<Integer>>();

				// holds sets for each dynamic constraint
				ArrayList<HashSet<Integer>> dynamicConstraintSets = new ArrayList<HashSet<Integer>>();

				// static and dynamic constraints need different evaluation mechanisms
				for (Constraint constraint : allConstraints) {
					if (constraint instanceof StaticConstraint && constraint.isActive()) {
						staticConstraintSets.add(evaluateStaticConstraint((StaticConstraint) constraint));
					} else if (constraint instanceof DynamicConstraint && constraint.isActive()) {
						dynamicConstraintSets.add(evaluateDynamicConstraint((DynamicConstraint) constraint));
					}
				}

				// intersection on all constraints in a group
				HashSet<Integer> intersectionSet = new HashSet<Integer>(
						(int) ((staticConstraintSets.size() + dynamicConstraintSets.size()) * 1.2f), 1.f);

				// union on all static constraints
				for (HashSet<Integer> staticHs : staticConstraintSets) {
					intersectionSet.addAll(staticHs);
				}

				// intersection on all dynamic constraints
				for (HashSet<Integer> dynamicHs : dynamicConstraintSets) {
					if (intersectionSet.isEmpty()) {
						intersectionSet.addAll(dynamicHs);
					} else {
						intersectionSet.retainAll(dynamicHs);
					}
				}

				this.uniqGroupIds.put(group.getId(), intersectionSet);
			}
		}

		// finally, union on all groups
		for (HashSet<Integer> groupSet : this.uniqGroupIds.values()) {
			this.uniqIds.addAll(groupSet);
		}
	}

	/**
	 * Returns unique ids, selected by static constraints
	 *
	 * @param staticConstraint static constraints
	 *
	 * @return selected, unique ids
	 */
	private HashSet<Integer> evaluateStaticConstraint(StaticConstraint staticConstraint) {
		HashSet<Integer> constraintSet = new HashSet<Integer>(
				(int) (staticConstraint.getSelection().length * 1.2f),
				1.f);

		for (int id : staticConstraint.getSelection()) {
			constraintSet.add(id);
		}

		return constraintSet;
	}

	/**
	 * Returns unique ids, selected by dynamic constraints
	 *
	 * @param dynamicConstraint dynamic constraints
	 *
	 * @return selected, unique ids
	 *
	 * @throws DatabaseAccessException
	 */
	private HashSet<Integer> evaluateDynamicConstraint(DynamicConstraint dynamicConstraint)
			throws DatabaseAccessException {
		try {
			HashSet<Integer> constraintSet = new HashSet<Integer>((int) (this.allGroups.length * 1.2f), 1.f);
			Statement stmt = database.getConnection().createStatement();

			// select range, WHERE Feature Operator Value, e.g. WHERE "2" > 42.0013
			ResultSet rs = stmt.executeQuery(
					"SELECT Id FROM Objects WHERE \"" + dynamicConstraint.getFeature().getId() + "\" "
					+ operatorToString(dynamicConstraint.getOperator()) + " " + dynamicConstraint.getValue() + ";");

			while (rs.next()) {
				constraintSet.add(rs.getInt(1));
			}

			stmt.close();

			return constraintSet;
		} catch (SQLException e) {
			throw new DatabaseAccessException();
		}
	}

	/**
	 * Converts operator to string accepted by SQL
	 *
	 * @param operator operator that should be converted
	 * @return SQL string
	 */
	private static String operatorToString(Operator operator) {
		switch (operator) {
		case EQUAL:
			return "=";
		case NOT_EQUAL:
			// "<>" in some cases of SQL.. go figure
			return "!=";
		case LESS:
			return "<";
		case LESS_OR_EQUAL:
			return "<=";
		case GREATER:
			return ">";
		case GREATER_OR_EQUAL:
			return ">=";
		default:
			// well .. there isn't a meaningful default, but at least we try not to induce a crash
			return "=";
		}
	}

	/**
	 * This method is used to request data.
	 *
	 * When the method is called, it requests the {@link GroupController} and the {@link SubspaceController}, calculates
	 * the currently active {@link Subspace} and {@link Group} and gets the appropriate {@link Feature}s from the
	 * {@link Database} or the cache, depending on changes.
	 *
	 * @return A sorted list of {@link ElementData}
	 * @throws DatabaseAccessException
	 *             if read operation failed in {@link Database}.
	 */
	public synchronized ElementData[] getData() throws DatabaseAccessException {
		if (this.elementCache == null) {
			// build element cache
			try {
				this.buildCache();
			} catch (InterruptedException e) {
				this.elementCache = new ElementData[0];
				e.printStackTrace();
			}
		}

		return this.elementCache;
	}

	/**
	 * Queries database and fill cache with elements
	 *
	 * @return cached elements
	 * @throws DatabaseAccessException
	 *             if read operation failed in {@link Database}.
	 * @throws InterruptedException
	 *             if worker threads got interupted
	 */
	private ElementData[] buildCache() throws DatabaseAccessException, InterruptedException {
		// invalidate cache
		this.elementCache = null;

		// build group cache
		this.allGroups = this.groupController.getGroups();

		// combine normal features and color features
		ArrayList<Feature> combinedFeatures = new ArrayList<Feature>();
		combinedFeatures.addAll(Arrays.asList(subspaceController.getActiveSubspace().getFeatures()));

		// do not forget to pass the color features
		for (Group group : this.allGroups) {
			if (group.getColorFeature() != null && !combinedFeatures.contains(group.getColorFeature())) {
				combinedFeatures.add(group.getColorFeature());
			}
		}

		// convert
		Feature[] features = new Feature[combinedFeatures.size()];
		combinedFeatures.toArray(features);

		ElementData[] elements = new ElementData[0];

		if (features.length > 0) {
			String requiredFeatures = buildRequiredFeaturesString(features);

			// start evaluating constraints instead of creating unused ElementData objects
			this.evaluateConstraints();

			int count = this.uniqIds.size();

			// no groups availble, get all objects
			if (this.allGroups.length == 0 || this.anyGroupSelectsAllObjects()) {
				try {
					Statement stmt = database.getConnection().createStatement();

					// number of rows in the table Objects, needed for array creation
					ResultSet rs = stmt.executeQuery("SELECT COUNT(Id) FROM Objects;");

					rs.next();
					count = rs.getInt(1);
					stmt.close();
				} catch (SQLException e) {
					throw new DatabaseAccessException();
				}
			} else {
				// init worker array
				Integer[] integerArray = new Integer[count];
				this.uniqIds.toArray(integerArray);
				this.uniqWorkerArray = ArrayUtils.toPrimitive(integerArray);
			}

			elements = new ElementData[count];

			String sharedSql = "SELECT " + requiredFeatures + " FROM Objects ";

			// adjust number of workers to size of selection
			int adjustedNumberOfThreads = (count < this.numberOfThreads) ? count : this.numberOfThreads;

			// threaded implementation of bottleneck, for now only prints boundaries
			int rowsPerThread = (int) Math.ceil((double) count / adjustedNumberOfThreads);

			List<Callable<Object>> jobsQ = new ArrayList<Callable<Object>>(adjustedNumberOfThreads);

			for (int i = 0; i < adjustedNumberOfThreads; ++i) {
				// let thread i fill elements[start:end]
				int start = i * rowsPerThread;
				int end = Math.min((i + 1) * rowsPerThread, count);

				// fill job queue with specific workload
				jobsQ.add(Executors.callable(new DataArrayWorker(this.database, sharedSql, elements,
						this.subspaceController.getCalculateEffectiveOutliernessBy(), this.allGroups, features, start,
						end, this.uniqWorkerArray, this.uniqGroupIds)));
			}

			// synchronization: execute and wait on all jobs
			this.xServ.invokeAll(jobsQ);

			// clean group cache
			this.allGroups = null;
		}

		// clear worker sets / arrays
		this.uniqGroupIds.clear();
		this.uniqIds.clear();
		this.uniqWorkerArray = null;

		// fill cache
		this.elementCache = elements;

		return this.elementCache;
	}

	/**
	 * Checks, if the given groups selects all objects
	 * Any group thats vsible and has no constraints selects all objects
	 *
	 * @param group group to check
	 * @return {@code true} if the group selects all objects, {@code false} otherwise
	 */
	private static boolean groupSelectsAllObjects(Group group) {
		boolean isSelected = false;

		if (group.isVisible() && group.getConstraints().length == 0) {
			isSelected = true;
		}

		return isSelected;
	}

	/**
	 * Checks, if there is a group that selects all objects
	 *
	 * @return {@code true} if there a a group, {@code false} otherwise
	 */
	private boolean anyGroupSelectsAllObjects() {
		boolean selectsAll = false;

		for (Group group : this.allGroups) {
			if (groupSelectsAllObjects(group)) {
				selectsAll = true;
			}
		}

		return selectsAll;
	}

	/**
	 * This method builds a String with all required features, to insert into the sql query
	 *
	 * @param features
	 *            a list of features
	 * @return the build string
	 */
	private static String buildRequiredFeaturesString(Feature[] features) {
		// SELECT " + requiredFeatures + " FROM Objects;
		// requiredFeatures has to be: "1", "3", "6", ..

		StringBuilder strB = new StringBuilder();
		for (Feature currentFeature : features) {
			strB.append("\"");
			strB.append(currentFeature.getId());
			strB.append("\"");
			strB.append(',');
		}
		strB.deleteCharAt(strB.lastIndexOf(","));

		return strB.toString();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		try {
			this.buildCache();

			this.setChanged();
			this.notifyObservers();
		} catch (DatabaseAccessException e) {
			// do not notify observers
			e.printStackTrace();
		} catch (InterruptedException e) {
			// do not notify observers
			e.printStackTrace();
		}
	}
}
