# HIS Rollback Guide

## Scope
- Service rollback
- Gateway route rollback
- Config rollback

## Steps
1. Stop impacted service instances.
2. Revert image/tag to previous stable version.
3. Revert Nacos config by history version.
4. Validate health endpoint and smoke APIs.
5. If DB schema was changed, execute restore script from a consistent backup snapshot.

## Verification
- `/actuator/health` is UP for all critical services
- Gateway routes return 200/业务码正常
- Zipkin trace continuity restored
