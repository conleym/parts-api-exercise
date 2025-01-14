# See https://hub.docker.com/_/postgres
# and https://github.com/docker-library/postgres/blob/0b87a9bbd23f56b1e9e863ecda5cc9e66416c4e0/17/bookworm/Dockerfile
FROM postgres:17.2

# Copy init files. .sql (and gzipped sql and .sh, but we're not using those) files run in order by name.
COPY init/* /docker-entrypoint-initdb.d/



