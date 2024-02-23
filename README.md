# Overall

Fpl-data fetches data from the Fantasy Premier League servers, cleans and transforms the data, and then stores it in MySQL and Redis.


Data processing occurs in three scenarios: every day, match day, and every gameweek, implemented using Spring Boot schedules for these tasks.


In the initial stages, the services in this project were originally designed within the FPL project. As the project expanded, these services were extracted and relocated to this dedicated project, a strategic move aimed at enhancing maintenance and scalability.


At present, these two projects operate independently, with Fpl-data handling data, while FPL is responsible for delivering services to users. This architectural setup has proven effective for over two years.


# Tech Stack


## Backend

- **[Java 20](https://www.java.com/en/)** + **[Spring Boot 3](https://spring.io/projects/spring-boot)**

- **[MySQL](https://www.mysql.com/)** for data storage

- **[Redis](https://redis.io/)** for caching

- **[Maven](https://maven.apache.org/)** for build and dependency management

- **[Logback](https://github.com/qos-ch/logback)** for logging

- **[Jasypt](https://github.com/ulisesbocchio/jasypt-spring-boot)** for encryption and decryption of sensitive data

- **[Jenkins](https://www.jenkins.io/)** for CI/CD

- **[Thymeleaf](https://www.thymeleaf.org/)** for server-side template


## Frontend

- **[Layui](https://github.com/layui/layui)** for user interface

- **[Axios](https://axios-http.com/)** for HTTP Client for browser

- HTML5 + JQUERY + CSS3

# LetLetMe Universe Architecture

```mermaid
flowchart LR
    subgraph client
    C1([Web])
    C2([MiniProgram])
    end
    subgraph Service
    S1(FPL)
    S2(Fpl-data)
    S3(Fpl-audit)
    S4(Telegram_bot)
    end
    subgraph DB
    D1[(MySQL)]
    D2[(Redis)]
    end
    subgraph External Resources
    R1{{FPL Server}}
    R2{{Telegram}}
    end
    C1-- request backend -->S1
    C2-- request backend -->S1
    S1-- request DB -->D1
    S1-- request DB -->D2
    S1-- request notification -->S4
    S4-- notify -->R2
    S2-- fetch -->R1
    S2-- persist -->D1
    S2-- persist -->D2
    S3-- audit -->D1
```