FROM gvenzl/oracle-xe:21-slim

USER root

COPY docker/oracle/init/ /container-entrypoint-initdb.d/
COPY db/schema/ /opt/tune-share-hub/schema/

RUN chmod +x /container-entrypoint-initdb.d/*.sh

USER oracle
