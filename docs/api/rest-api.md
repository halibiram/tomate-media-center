# Tomato REST API

This document describes the REST API provided by Tomato for remote interaction and integration with other services. (If applicable, otherwise this can be removed or repurposed).

## Overview

- Authentication
- Rate Limiting
- Versioning

## Endpoints

### Library Management
- `GET /api/library/movies`
- `POST /api/library/scan`

### Playback Control
- `GET /api/player/status`
- `POST /api/player/play`
- `POST /api/player/pause`
- `POST /api/player/seek`

### System Information
- `GET /api/system/info`

## Error Codes

- Common HTTP Status Codes
- Application-Specific Error Codes

## Client Libraries (If any)
md
File 'docs/api/rest-api.md' created successfully.
