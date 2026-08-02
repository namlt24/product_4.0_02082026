# Generated Service Package

Replace `com.viettel.bccs/organization` with the generated root package.
Create packages by business feature and then by responsibility:

```text
<feature>
├── controller
├── dto/request
├── dto/response
├── service
├── repository
├── entity
├── model
├── mapper
├── client
├── cache
├── event
└── metrics
```

Only create packages the feature uses. Controllers depend on same-feature
services; services orchestrate repositories, clients, cache, events, metrics,
and mappers. Keep entities out of controller signatures and keep shared platform
behavior in approved BCCS starters.
