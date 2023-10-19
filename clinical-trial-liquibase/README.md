[![Lint](https://github.com/projectronin/clinical-one-service/actions/workflows/lint.yml/badge.svg)](https://github.com/projectronin/clinical-one-service/actions/workflows/lint.yml)

# clinical-trial-liquibase

Provides Liquibase changelogs defining the Clinical Trial database.

## Conventions

### Indexes and Constraints ###

All names should be written in lowercase.

Note that there is a 64-character limit for MySQL; therefore, any examples that may exceed that limit should have
appropriate truncation or name simplification done such that they are still easy to follow.

#### Primary Key ####

pk_TABLE

#### Foreign Key ####

fk_TABLE_REFERENCEDTABLE

#### Unique ####

uk_TABLE_COLUMN uk_TABLE_COLUMN1_COLUMN2...

## Docker Support

This project is available in Docker format for facilitating liquibase operations, such as schema updates. The docker
image builds on the official [liquibase docker image](https://github.com/liquibase/docker)

### Building the Docker Container Image

```shell
docker image build -t clinical-trial-liquibase . 
```
