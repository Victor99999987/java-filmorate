# java-filmorate
ER-диаграмма проекта
![](/er_diagram/erd.png)

Данная схема позволяет делать основные запросы. Примеры запросов.

GET /users/{id}/friends

Получить список друзей по {id} пользователя
```
SELECT *
FROM users
WHERE users.id IN (
                   SELECT friend_id
                   FROM friendship
                   WHERE users_id = {id}
                   AND confirmed = true
                 );
```

Получение списка список друзей, общих с другим пользователем

GET /users/{id}/friends/common/{otherId} 

```
SELECT *
FROM users
WHERE users.id IN (
                   SELECT friend_id 
                   FROM friendship 
                   WHERE users_id = {id}
                   AND confirmed = true 
                   AND friend_id IN (
                                     SELECT friend_id 
                                     FROM friendship 
                                     WHERE users_id = {otherId}
                                     AND confirmed = true 
                                    )
                  );
```

Возвращает список из первых count фильмов по количеству лайков.

GET /films/popular?count={count}

```
SELECT films.id,
       films.name,
       films.description,
       films.releaseDate,
       films.duration,
       ratings.name AS rating,
       COUNT(films.id) AS likes
FROM films
INNER JOIN likes ON films.id = likes.films_id
INNER JOIN users ON users.id = likes.users_id
INNER JOIN ratings ON ratings.id = films.ratings_id
GROUP BY films.id
ORDER BY likes DESC
LIMIT {count}
```