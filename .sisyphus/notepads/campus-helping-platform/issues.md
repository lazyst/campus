# Campus Helping Platform - Implementation Issues

## Resolved Issues

### LSP Errors vs Compilation
- **Issue**: IDE shows many LSP errors related to Lombok (@Data, @TableName)
- **Resolution**: These are IDE-specific and don't affect compilation
- **Verification**: `mvn compile` succeeds (67 source files)

### Reserved Keywords in MySQL
- **Issue**: `like` and `collect` are reserved SQL keywords
- **Resolution**: Escape table names with backticks: `@TableName("`like`")`

### Boolean vs Integer for deleted Field
- **Issue**: BaseEntity uses Integer for `deleted` field
- **Error**: `incompatible types: boolean cannot be converted to java.lang.Integer`
- **Resolution**: Use `item.setDeleted(1)` instead of `item.setDeleted(true)`

### Type Comparison Issue
- **Issue**: Comparing Integer with boolean in filter lambda
- **Error**: `incompatible types: java.lang.Integer cannot be compared to boolean`
- **Resolution**: Use `item.getDeleted() == 0` instead of `item.getDeleted() == false`

## Pre-existing Issues (Not from this session)

### AuthTest Failures
- **Issue**: H2 database table "USER" not found
- **Root Cause**: Test database initialization issue
- **Impact**: AuthTest fails, but ApplicationTest passes (2/2)
- **Note**: This is a pre-existing issue from earlier sessions

## Files Modified This Session

### Forum Module
- `entity/Collect.java` (NEW)
- `mapper/CollectMapper.java` (NEW)
- `service/CollectService.java` (NEW)
- `service/impl/CollectServiceImpl.java` (NEW)
- `controller/CollectController.java` (NEW)
- `entity/Notification.java` (NEW)
- `mapper/NotificationMapper.java` (NEW)
- `service/NotificationService.java` (NEW)
- `service/impl/NotificationServiceImpl.java` (NEW)
- `controller/NotificationController.java` (NEW)

### Trade Module
- `service/ItemService.java` (MODIFIED - added status management methods)
- `service/impl/ItemServiceImpl.java` (MODIFIED - added status management)
- `controller/ItemController.java` (MODIFIED - added status endpoints)
- `entity/ItemCollect.java` (NEW)
- `mapper/ItemCollectMapper.java` (NEW)
- `service/ItemCollectService.java` (NEW)
- `service/impl/ItemCollectServiceImpl.java` (NEW)
- `controller/ItemCollectController.java` (NEW)
