to init INFRA:

1. run:
   docker compose exec postgres chmod +x /docker-entrypoint-initdb.d/init-multiple-databases.sh
2. docker compose up -d
3. init DB:
   cd database/db/changelog/employee
   docker build -f Dockerfile-liquibase -t liquidbase-employee .
4. update DB:
   docker run --rm --network=wms-employee_microservices-network --name liquibase-employee-run liquidbase-employee
   --defaultsFile=/liquibase/liquibase-employee.properties update
   docker run --rm --network=wms-employee_microservices-network --name liquibase-employee-run liquidbase-employee
   --defaultsFile=/liquibase/liquibase-employee.properties dropAll

docker run --rm -it liquidbase-employee bash 

