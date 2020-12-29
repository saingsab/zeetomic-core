#!/bin/bash
# A simple script for mocking api of indracore
# 1 Check Balance 
for ((i = 0 ; i < 10000 ; i++)); do
   echo $(date '+%Y/%m/%d %H:%M:%S') $(curl -X GET "http://localhost:3001/pub/v1/portforlio" -H  "accept: application/json" -H  "authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJfaWQiOiJjZTk1ZTUxNi1jYzU4LTRiYjAtODQ1Yy0wYjc3YTNiODI3N2IiLCJleHAiOjE2MDkzMDQ5MzJ9.6H2kMvsNOeA9wO0mK92Pe542zi1jRwxurYTlIXml4Yo")  >> ./mock_query_balance.log
done

# 2 Transfer
# for ((i = 0 ; i < 1000 ; i++)); do
#    echo $(date) $(curl -X GET "http://localhost:3001/pub/v1/portforlio" -H  "accept: application/json" -H  "authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJfaWQiOiJjZTk1ZTUxNi1jYzU4LTRiYjAtODQ1Yy0wYjc3YTNiODI3N2IiLCJleHAiOjE2MDkzMDQ5MzJ9.6H2kMvsNOeA9wO0mK92Pe542zi1jRwxurYTlIXml4Yo")  >> ./mock_transfer_balance.log
# done

# 3  Check Balance  from API KEY
# 4  Transfer  from API KEY