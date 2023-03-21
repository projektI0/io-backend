# Shops in Kraków

Shops have been taken from overpass api via curl below

```bash
curl --location 'https://overpass-api.de/api/interpreter' \
--header 'Accept: application/json' \
--header 'Accept-Language: pl-PL,pl;q=0.9,en-US;q=0.8,en;q=0.7' \
--header 'Connection: keep-alive' \
--header 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8' \
--header 'Origin: https://overpass-turbo.eu' \
--header 'Referer: https://overpass-turbo.eu/' \
--header 'Sec-Fetch-Dest: empty' \
--header 'Sec-Fetch-Mode: cors' \
--header 'Sec-Fetch-Site: cross-site' \
--header 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36' \
--header 'sec-ch-ua: "Google Chrome";v="111", "Not(A:Brand";v="8", "Chromium";v="111"' \
--header 'sec-ch-ua-mobile: ?0' \
--header 'sec-ch-ua-platform: "Linux"' \
--data-urlencode 'data=[out:json];area[name="Kraków"];nwr[shop](area);out center;'
```