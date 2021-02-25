# Observations
* current bundled version of `r2dbc-postgres`, `0.8.6.RELEASE` does not support relative or classpath values for `sslRootCert`.  This value must be an absolute path :(.  `sslRootCert=classpath:ca.crt` does not work.  It appears this has been fixed in the `0.8.7.RELEASE`.

* specifying the database/cluster name as part of `options=--cluster=${cluster_name}` does not work with CC free tier.  Must specify the database name prepended to `defaultdb` instead.