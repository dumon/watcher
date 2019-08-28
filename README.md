# Network device scanner web-application

## Description
One-page application shows devices in local network.
Included features:
 - HATEOAS access for RESTful resources (devices)
 - Secured access with configurable users/roles
 - UI: Spring MVC + JSP + Bootstrap
 - Configurable parameters like are network/pinging/scanSchedule
 
## Application usage
 * Build: mvn clean install
 * Run: java -jar watcher-<VERSION>.war
 * Navigate to: http://localhost:8080
   
## Configurable options
1. Application config
   - Application config (app.config) could be placed to dir from where war will be run.
   - Or path for config could be passed as JVM arg:
     java -jar -Dapp.config=../cfg/app.config watcher-<VERSION>.war
   - Here are full list of configurable options (validated):
     app.watcher.ping-timeout=2000
     app.watcher.scan-interval=1800000
     app.watcher.local-ip=10.0.0.1
2. Users also could be provided in separated files (json format) by JVM arg:
   java -jar -Dusers=../cfg/users.json watcher-<VERSION>.war
   Two roles are predefined: ADMIN/USER. File must be follow format like that:
   [
     {
       "login": "admin",
       "pass": "123",
       "role": "ADMIN"
     },
     {
       "login": "user",
       "pass": "123",
       "role": "USER"
     }
   ]
## Misc. H2 database console accessible through:
   http://localhost:8080/console
   login 'dimon' with empty password