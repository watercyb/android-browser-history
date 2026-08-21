# Android Browser History

An Android browser history management library/application designed to efficiently store, search, index, and manage browsing history.

The project focuses on fast in-memory history operations, persistent SQLite storage, URL token indexing, and concurrent access handling.

## Features

- **Browsing history management**
  - Add and remove visited URLs
  - Store URL, title, timestamp, and history state
  - Prevent duplicate URL entries while preserving history information

- **Fast history search**
  - Search URLs and titles
  - Support empty searches to retrieve all active history
  - Token-based URL indexing for faster searches

- **URL tokenization**
  - Break URLs into searchable components such as:
    - `google`
    - `search`
    - `query`
  - Maintain an index for efficiently finding matching history entries

- **Persistent storage**
  - SQLite database for storing browsing history
  - Separate in-memory data structures for fast access

- **Concurrent access**
  - Thread-safe history operations
  - Locking around operations that modify shared history data
  - Coroutine-based background database operations

- **Deletion handling**
  - Support logical removal of history entries
  - Keep in-memory structures synchronized with persistent storage

## Architecture

The project is organized into several components for managing browsing history, search, token indexing, and favicon storage.

### `BrowsingHistory`

High-level interface for browsing history operations and URL search suggestions.

- `BrowsingHistory.initialize()` — Initialize the browsing history system.
- `BrowsingHistory.searchHistory(query: String)` — Search for and suggest URLs based on the input query.
- `BrowsingHistory.clear()` — Clear all browsing history.
- `BrowsingHistory.deleteHistory(historyURL: HistoryUrl)` — Delete a history record.
- `BrowsingHistory.restoreHistory(historyURL: HistoryUrl)` — Restore a previously deleted history record.
- `BrowsingHistory.getList()` — Get the current history list.

### `HistoryURLs`

Maintains the browsing history using:

- A `URL → HistoryURL` map for fast URL lookup.
- An ordered history list for maintaining history order.

### `HistoryTokens`

Maintains a token index:

- `Token → List<HistoryURL>` map.
- Used to efficiently find history records associated with URL tokens.

### `HistoryTrie`

Maintains a trie tree of tokens:

- Each token is stored in the trie.
- Trie nodes contain associated `List<HistoryURL>`.
- Used for efficient prefix-based URL suggestions.

### `utils/Favicon`

Handles favicon storage and caching for history URL records.

- Saves favicons to files associated with URL records.
- Maintains an in-memory favicon cache for fast access.
- `Favicon.initialize()` — Initialize the favicon storage and cache system.

### `SQLite`

Handles persistent storage of browsing history using SQLite.

- Saves and loads only history from the past **one year**.
- Maintains a maximum of `MAX_HISTORY_COUNT = 100000` history records to limit memory usage.
- `BrowsingHistory.deleteHistory()` marks a record as removed using `HistoryUrl.isRemoved`.
- `BrowsingHistory.restoreHistory()` removes the deletion mark and restores the record.
- Records marked as removed are permanently deleted from SQLite the next time the app starts and loads the history.

## Search

The search system supports both general history retrieval and keyword-based searches.

For example:

```text
https://www.google.com/search?q=android
```

can be tokenized into searchable components such as:

```text
google
search
android
```

The token index can then be used to locate history entries without scanning the entire history collection for every search.

## Thread Safety

History data can be accessed from multiple threads, particularly when database operations and UI operations happen concurrently.

The project therefore uses locking around critical operations to protect shared data structures.

Long-running database operations are performed using Kotlin coroutines and appropriate background dispatchers.

## Technology

- **Kotlin**
- **Android**
- **SQLite**
- **Kotlin Coroutines**
- **Java/Kotlin Collections**
- **Gradle**

## Project Structure

This repository is a **complete, runnable Android project**. It can be opened directly in Android Studio and run on an Android emulator or physical Android device.

### Main Components

```text
history/
├── history/
│   ├── SQLite/
│   │   └── ...                 # SQLite history storage
│   │
│   ├── utils/
│   │   └── ...                 # Utility components, including Favicon               
│   │
│   ├── historypage/
│   │   └── ...                 # Example history page Activity
│   │
│   └── ...                     # Core browsing history implementation
│
└── ...                         # Example Android application: Main Activity, URL bar, WebView, etc.
```

## Getting Started

### Requirements

- Android Studio
- Android SDK
- JDK compatible with the project's Gradle configuration

### Clone the Repository

```bash
git clone https://github.com/watercyb/android-browser-history.git
```

## Design Goals

The project is designed around three main goals:

1. **Fast search** — minimize the amount of history that needs to be scanned during searches.
2. **Efficient storage** — use in-memory structures for frequent operations while keeping persistent data in SQLite.
3. **Safe concurrency** — prevent inconsistent history state when multiple operations occur simultaneously.

## Status

This project is under active development.

Current development focuses on improving:

- Search performance
- URL token indexing
- SQLite synchronization
- Concurrent history operations
- Memory efficiency

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for the full license text.
