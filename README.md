[![Build Status](https://travis-ci.org/pardom/ActiveAndroid.png?branch=master)](https://travis-ci.org/pardom/ActiveAndroid) [![Stories in Ready](https://badge.waffle.io/pardom/ActiveAndroid.png)](http://waffle.io/pardom/ActiveAndroid)  
# ActiveAndroid

ActiveAndroid is an active record style ORM ([object relational mapper](http://en.wikipedia.org/wiki/Object-relational_mapping)). What does that mean exactly? Well, ActiveAndroid allows you to save and retrieve SQLite database records without ever writing a single SQL statement. Each database record is wrapped neatly into a class with methods like _save()_ and _delete()_.

ActiveAndroid does so much more than this though. Accessing the database is a hassle, to say the least, in Android. ActiveAndroid takes care of all the setup and messy stuff, and all with just a few simple steps of configuration.

## Download

Grab via Maven:
```xml
<dependency>
  <groupId>com.michaelpardo</groupId>
  <artifactId>activeandroid</artifactId>
  <version>3.1.0-SNAPSHOT</version>
</dependency>
```
or Gradle:
```groovy
repositories {
    mavenCentral()
    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}

compile 'com.michaelpardo:activeandroid:3.1.0-SNAPSHOT'
```

## Documentation

* [Getting started](http://github.com/pardom/ActiveAndroid/wiki/Getting-started)
* [Creating your database model](http://github.com/pardom/ActiveAndroid/wiki/Creating-your-database-model)
* [Saving to the database](http://github.com/pardom/ActiveAndroid/wiki/Saving-to-the-database)
* [Querying the database](http://github.com/pardom/ActiveAndroid/wiki/Querying-the-database)
* [Type serializers](http://github.com/pardom/ActiveAndroid/wiki/Type-serializers)
* [Using the content provider](http://github.com/pardom/ActiveAndroid/wiki/Using-the-content-provider)
* [Schema migrations](http://github.com/pardom/ActiveAndroid/wiki/Schema-migrations)
* [Pre-populated-databases](http://github.com/pardom/ActiveAndroid/wiki/Pre-populated-databases)
* [Running the Test Suite](https://github.com/pardom/ActiveAndroid/wiki/Running-the-Test-Suite)

## License

[Apache Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)

    Copyright (C) 2010 Michael Pardo

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Contributing

Please fork this repository and contribute back using [pull requests](http://github.com/pardom/ActiveAndroid/pulls).

Any contributions, large or small, major features, bug fixes, unit tests are welcomed and appreciated but will be thoroughly reviewed and discussed.

You can run the test suite by following the instructions on the [Running the Test Suite](https://github.com/pardom/ActiveAndroid/wiki/Running-the-Test-Suite) Wiki page.


## Author

Michael Pardo | www.michaelpardo.com | www.activeandroid.com
