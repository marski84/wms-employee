to init INFRA:

1. run:
   docker compose exec postgres chmod +x /docker-entrypoint-initdb.d/init-multiple-databases.sh
2. docker compose up -d
3. init DB:
   cd database/db/changelog/employee
   docker build -f Dockerfile-liquibase -t employee-db .
4. update DB:

   docker run --rm \
   --network=wms-employee_microservices-network \
   --name liquibase-employee-run \
   employee-db \
   update \
   --defaultsFile=/liquibase/liquibase-employee.properties \
   --changelog-file=/changelog/employee/db.changelog-master.xml

   DROP:

   docker run --rm \
   --network=wms-employee_microservices-network \
   --name liquibase-employee-run \
   employee-db \
   dropAll \
   --defaultsFile=/liquibase/liquibase-employee.properties

docker run --rm -it employee-db bash 

