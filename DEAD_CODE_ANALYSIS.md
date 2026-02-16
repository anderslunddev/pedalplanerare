# Dead Code Analysis

## Unused Methods Found

### 1. `BoardRepositoryAdapter.findAll()`
**Location:** `BoardRepositoryAdapter.java:36-40`
**Status:** ❌ UNUSED
**Reason:** No calls found in codebase

### 2. `BoardRepositoryAdapter.existsById()`
**Location:** `BoardRepositoryAdapter.java:52-54`
**Status:** ❌ UNUSED
**Reason:** No calls found in codebase

### 3. `BoardRepositoryAdapter.getPedalsForBoard()`
**Location:** `BoardRepositoryAdapter.java:60-67`
**Status:** ❌ UNUSED
**Reason:** No calls found in codebase. Pedals are loaded via `Board.pedals()` from `findById()`

### 4. `UserRepositoryAdapter.findById()`
**Location:** `UserRepositoryAdapter.java:27-29`
**Status:** ❌ UNUSED
**Reason:** No calls found in codebase

### 5. `PedalRepository.existsByBoardAndPlacement()`
**Location:** `PedalRepository.java:9`
**Status:** ❌ UNUSED
**Reason:** No calls found in codebase. Placement validation is handled in domain layer (`Board.resolvePlacementFor()`)

## Methods That ARE Used (Keep These)

- ✅ `PedalRepositoryAdapter.findById(UUID)` - Used in `CableService.createCable()`
- ✅ All other methods are actively used

## Recommendation

Remove the 5 unused methods listed above to reduce code complexity and maintenance burden.
