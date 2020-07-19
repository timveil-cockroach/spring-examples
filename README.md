# CockroachDB + Spring Boot Examples

Including isolation level in annotation adds the following check during queries...

```
.core.v3.QueryExecutorImpl   :  <=BE ReadyForQuery(I)
2020-07-19 19:20:30.663 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :   simple execute, handler=org.postgresql.jdbc.PgStatement$StatementResultHandler@70028793, maxRows=0, fetchSize=0, flags=17
2020-07-19 19:20:30.663 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Parse(stmt=null,query="SHOW TRANSACTION ISOLATION LEVEL",oids={})
2020-07-19 19:20:30.663 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Bind(stmt=null,portal=null)
2020-07-19 19:20:30.663 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Describe(portal=null)
2020-07-19 19:20:30.664 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Execute(portal=null,limit=0)
2020-07-19 19:20:30.664 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  FE=> Sync
2020-07-19 19:20:30.670 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE ParseComplete [null]
2020-07-19 19:20:30.670 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE BindComplete [unnamed]
2020-07-19 19:20:30.670 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE RowDescription(1)
2020-07-19 19:20:30.671 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :         Field(transaction_isolation,TEXT,65535,T)
2020-07-19 19:20:30.671 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE DataRow(len=12)
2020-07-19 19:20:30.671 TRACE 23257 --- [           main] o.postgresql.core.v3.QueryExecutorImpl   :  <=BE CommandStatus(SHOW 1)
```

Recommendation is to set isolation level in hikari an not at annotation

`spring.datasource.hikari.transaction-isolation=TRANSACTION_SERIALIZABLE` not `@Transactional(isolation = Isolation.SERIALIZABLE)`

