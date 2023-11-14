package com.yuriisurzhykov.ksolidhsm.exceptions

/**
 *  This error indicates if there is a cross-reference between child and parent. Cross-reference
 *  means that child has parent, but parent also specifies it's own parent but it is child class,
 *  so they cross-reference to each other.
 * */
class RecursiveHierarchyException(
    child: Any,
    parent: Any
) : IllegalStateException("Child $child infer to parent $parent, but parent infer to child! Check your hierarchy of states!")