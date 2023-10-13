# rtw
Read-Transform-Write is a pipeline style library for writing components to separate reads, writes, and non-db operations (transforms) in separate stages that each use their own operation context (for example, a different database connection).  Helps with writer/reader separated arquitectures such as Amazon RDS clusters
