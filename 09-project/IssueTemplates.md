# PushPal — Issue Templates

## Feature Issue Template

```markdown
### Title
[Feature]: <short description>

### Epic
<epic label>

### Priority
<critical|high|medium|low>

### Area
<backend|frontend|infra|push|auth|db>

### Description
<What is this feature? Why do we need it?>

### Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2
- [ ] Criterion 3

### Technical Notes
<Implementation details, API changes, schema changes>

### Dependencies
<List any issues that must be completed first>

### UI Mockup (if applicable)
<Description or link to mockup>

### Testing Notes
<How to test this feature>
```

---

## Bug Issue Template

```markdown
### Title
[Bug]: <short description>

### Priority
<critical|high|medium|low>

### Area
<backend|frontend|push|auth|db>

### Description
<What is the bug?>

### Steps to Reproduce
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

### Expected Behavior
<What should happen?>

### Actual Behavior
<What actually happens?>

### Screenshots
<If applicable, add screenshots>

### Environment
- Browser: <Chrome, Safari, etc.>
- Device: <iPhone, Android, Desktop>
- OS: <iOS 17, Android 14, etc.>

### Additional Context
<Any other information that might help>
```

---

## Task Issue Template

```markdown
### Title
[Task]: <short description>

### Type
<chore|docs|test>

### Priority
<high|medium|low>

### Area
<backend|frontend|infra|quality>

### Description
<What needs to be done?>

### Checklist
- [ ] Step 1
- [ ] Step 2
- [ ] Step 3

### Notes
<Additional context or instructions>
```

---

## Example Issues

### Feature Example

```markdown
### Title
[Feature]: Add notification cancel button

### Epic
Self-Reminders

### Priority
Medium

### Area
Backend + Frontend

### Description
Users should be able to cancel a pending notification before it's delivered.

### Acceptance Criteria
- [ ] Cancel button visible on pending notifications
- [ ] Confirmation dialog before cancelling
- [ ] Status changes to CANCELLED on confirm
- [ ] Cancelled notifications not delivered
- [ ] Cancelled notifications still visible in list (greyed out)

### Technical Notes
- Add DELETE /api/notifications/:id endpoint
- Only allow cancellation when status is PENDING
- Update frontend NotificationCard to show cancel button
- Add confirmation dialog component

### Dependencies
None

### Testing Notes
- Create a notification scheduled for future
- Verify cancel button appears
- Click cancel, confirm in dialog
- Verify notification status changes to CANCELLED
- Verify notification is not delivered at scheduled time
```

### Bug Example

```markdown
### Title
[Bug]: Push notification not received on iOS Safari

### Priority
High

### Area
Push

### Description
Push notifications are not being received on iOS Safari PWA, even though the subscription was registered successfully.

### Steps to Reproduce
1. Open PushPal on iOS Safari
2. Install PWA to home screen
3. Open PWA and register for push notifications
4. Schedule a notification for 1 minute from now
5. Wait for scheduled time
6. No notification appears

### Expected Behavior
Push notification should appear at scheduled time

### Actual Behavior
No notification appears

### Environment
- Browser: Safari 17.2
- Device: iPhone 15
- OS: iOS 17.2

### Additional Context
- Subscription registration returns 201
- Backend logs show notification status as SENT
- Works correctly on Chrome Android
```
