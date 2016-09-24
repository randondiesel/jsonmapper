# About JSON Mapper

JSON is a lightweight library to convert between JSON encoded data and Java
objects. It relies only on reflection, generics and runtime annotations to
perform the conversions.

JSON Mapper builds on top of [JSON-java](https://github.com/stleary/JSON-java),
which is a reference implementation of a JSON package in Java.


# Using JSON Mapper

The JSON to Java object converter is implemented as
`com.hashvoid.jsonmapper.decode.Json2Object`. The corresponding conversion
from Java  object to JSON is implemented as
`com.hashvoid.jsonmapper.encode.Object2Json`.

Consider the following data object, which is a POJO :

```java
public class UserProfile {

    @JSON("firstname")
    private String firstName;

    @JSON("lastname")
    private String lastName;

    @JSON("dobday")
    private int day;

    @JSON("dobmonth")
    private int month;

    @JSON("dobyear")
    private int year;
}
```

Methods to set the values into these fields have not shown above for purpose of
brevity. Now consider the following JSON string:

```json
{
  "firstname": "Randon",
  "lastname": "Diesel",
  "dobday": 12,
  "dobmonth": 5,
  "dobyear": 2001
}
```
The code to load this JSON into `UserProfile` is as follows:

```java
byte[] jsonStr = new byte[1024];

// Write the logic to populate this array with the JSON data, e.g. by reading
// from a file.

UserProfile uprof = new Json2Object().convert(jsonStr, UserProfile.class);

```

The reverse implementation, to write out the contents of an already populated
UserProfile into a JSON string, is as follows:

```java
UserProfile uprof = new UserProfile();

// Write the logic to populate the fields of the instance, e.g. by calling
// bean-style setter methods defined by the UserProfile class.

byte[] jsonStr = new Object2Json().convert(uprof);
```

