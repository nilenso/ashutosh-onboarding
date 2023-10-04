# Clinic

The software system is built for an orthopedic clinic to keep the scope of this
project narrow. The clinics using this system only provide treatments for the
following condition.

- Back pain
- Bone fracture
- Arthritis
- Sprains
- Joint pain
- Injuries to tendons and ligaments

## Developer Quick Start

### Prerequisites

1. [A JDK](https://openjdk.org/)
2. [Clojure](https://clojure.org/guides/install_clojure)
3. [Leiningen](https://codeberg.org/leiningen/leiningen)
4. [Docker Compose](https://docs.docker.com/compose/install/)

### Installing Dependencies

```console
npm install
lein deps
```

### Running Required Services

Clinic needs a running instance of HAPI FHIR. Use `docker-compose` to quickly
spin up a HAPI FHIR instance (port 8080) with PostgreSQL storage backend (port
5432) and a pgAdmin instance (port 5433) using bridge networking with the host.

```console
docker-compose up -d
```
