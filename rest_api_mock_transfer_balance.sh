#!/bin/bash
# A simple script for mocking api of indracore
# Transfer Balance
for ((i = 0 ; i < 1000 ; i++)); do
   sleep 7
   echo $(date '+%Y/%m/%d %H:%M:%S') $(curl -X POST "http://localhost:3001/pub/v1/sendpayment" -H  "accept: application/json" -H  "authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJfaWQiOiJjZTk1ZTUxNi1jYzU4LTRiYjAtODQ1Yy0wYjc3YTNiODI3N2IiLCJleHAiOjE2MDkzMDQ5MzJ9.6H2kMvsNOeA9wO0mK92Pe542zi1jRwxurYTlIXml4Yo" -H  "Content-Type: application/json" -d "{  \"pin\": \"1234\",  \"asset_code\": \"SEL\",  \"destination\": \"5Ek83gXtJGzRokYvWW9TmTPmSHWk3T6cUijukuvoMrVD2hDj\",  \"amount\": \"1\",  \"memo\": \"TEST\"}")  >> ./mock_transfer_balance.log
done