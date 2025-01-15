package edu.mit.jwi.data

/**
 * A data source that is also loadable.
 */
interface ILoadableDataSource<T> : IDataSource<T>, ILoadable

