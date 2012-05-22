Subspaces Datasheet File (SSD)
==============================

Introduction
------------

This file describes the layout of SSDs. It was created with the visualization
tool Black Sheep Vision (BSV).

A SSD contains collected informations computed by a data mining algorithm.
That means a SSD contains all subspaces where potentially outliers are detected
by a data mining algorithm and a value, called outlierness value, which says if 
the object a is potential outlier in subspace b or not.

So a SSD is composed of two parts. In the first part are all subspaces are
indexed where are potential outliers got detected. And in the second part are
listed all outlierness values of each object in the corresponded subspaces.


Format
------

SSD are an extended csv format which is composed by two parts.

In part one all subspaces detected by a data mining algorithm will be indexed.
A valid index entry in part one consits of the keyword "subspace" followed by
its index followed by the keyword "dimension" followed by its dimension and 
followed by a base consits of list with all features, seperated with a comma,
spanning this subspaces bracketed with squared braces.

Whitespaces in the base of the indexed subspaces are optional.

After that part two will be introduced with a "@data" tag followed by all
outlierness values, computed by the same data mining algorithm which found
the subspaces indexed in part one.
A valid entry in part two consits of the "object id", the "subspace id" and 
the outlierness value of the object with "object id" in subspace "subspace id".
All values have to be seperated by a valid delimiter of the csv format,
that means valid you can use ',', ';', ':', ' ' or a tab as delimiter.

If you're using ',', ';' or ':' as delimiter all whitespaces are optional.


General SSD Layout
------------------

Here we see the general structure of a valid SSD.

    subspace 0 dimension = x [F1, F2, ..., Fx]
      .		.
      .		.
      .		.
      .		.
    subspace n dimension = y [P1, P2, ..., Py]
    
    @data
    obj_0; subspace_0; <<Detected Outlierness of obj_0 in subpsace_0>>
    obj_0; subspace_1; <<Detected Outlierness of obj_0 in subpsace_1>>
      .	.		.
      .	.		.
      .	.		.
    obj_0; subspace_n; <<Detected Outlierness of obj_0 in subpsace_n>>
      .	.		.
      .	.		.
      .	.		.
    obj_i; subspace_0; <<Detected Outlierness of obj_i in subpsace_0>>
    obj_i; subspace_1; <<Detected Outlierness of obj_i in subpsace_1>>
      .	.		.
      .	.		.
      .	.		.
    obj_i; subspace_n; <<Detected Outlierness of obj_i in subpsace_n>>
