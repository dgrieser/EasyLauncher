#!/bin/bash
start_date="2025-01-01"
end_date="2025-12-31"

current_date="$start_date"
while [[ "$current_date" != "$end_date" ]]; do
  daily-text --verse-only "$current_date"
  current_date=$(date -I -d "$current_date + 1 day")
done
daily-text --verse-only "$end_date"

exit 0
