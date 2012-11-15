ActiveAndroid is an active record style ORM (object relational mapper). What does that mean exactly? Well, ActiveAndroid allows you to save and retrieve SQLite database records without ever writing a single SQL statement. Each database record is wrapped neatly into a class with methods like save and delete.

Instead of

```sql
INSERT INTO Items(id, name) VALUES(NULL, 'My Item');
```

we write

```java
Item item = new Item();
item.name = "My Item";
item.save();
```

and querying the database should feel familiar

```sql
SELECT * FROM Items;
```

is now

```java
new Select().from(Item.class).execute();
```

ActiveAndroid does so much more than this though. Accessing the database is a hassle, to say the least, in Android development. ActiveAndroid takes care of all the setup and messy stuff, and all with just two simple steps of configuration.

ActiveAndroid is easy!