# FindHata
Приложение для поиска и размещения объявлений о недвижимости. 
Для добавления своего объявления пользователь может добавить описание своей недвижимости и приложить необходимые фотографии.
Для поиска подходящего объявления пользователю предлагается поисковая строка.

### Фунционал:
  - Регистрация на сайте
  - Подтверждение регистрации через почту
  - Добавление описания и фотографий к объявлению
  - Поиск объявлений по ключевому слову
  - Чат с мгновенными сообщениями между покупателем и продавцом
  - Автоматическая организация удалённого доступа к сайту через ngrok

### Требования:
  - Java 19 или новее (опционально)
  - Gradle 8.2.1 или новее (опционально)
  - Установленный Docker и Docker compose

### Запуск:
Добавьте переменную окружения NGROK_AUTHTOKEN
```bash
export NGROK_AUTHTOKEN={{token}}
```
Соберите и запустите приложение (Требуется Java и Gradle)
```bash
make build;
docker compose up
```
Также вы можете собрать проект внутри docker
```bash
docker compose -f docker-compose-build-in-docker.yml up
```

В локальной сети сайт использует 5000 порт.

Внешний адрес сайта можно узнать через веб-интерфейс ngrok, который использует 4040 порт.

### Скриншоты

  - Главное меню

![main_menu](./images/main_menu.png)

  - Страница входа

![sign_in](./images/sign_in.png)

  - Страница регистрации

![registration](./images/registration.png)

![registration](./images/registration_mes.png)

- Подтверждение почты

![registration](./images/mes.png)

- Добавление объявления

![registration](./images/add.png)

- Использование поисковой строки

![registration](./images/search.png)

- Подробная информация об объявлении и чат с продавцом

![registration](./images/chat.png)


- Чаты продавца с покупателями

![registration](./images/dialogs.png)
