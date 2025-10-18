# secondhand

docker run --name pg-secondhand -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=secondhand -p 5432:5432 -d postgres:16
