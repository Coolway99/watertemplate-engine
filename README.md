Water Template Engine
===

Water Template Engine is an open-source modern Java 8 template engine that simplifies the way you interact with templates.
With no external dependencies, it is very lightweight and robust.

Just like [mustache](https://mustache.github.io/), Water is a logic-less template engine, but it takes advantage of statically typed languages features to increase reliability and prevent errors.

[![Travis build on branch master](https://api.travis-ci.org/tiagobento/watertemplate-engine.svg?branch=master)](https://travis-ci.org/tiagobento/watertemplate-engine) [![Coverage Status](https://coveralls.io/repos/tiagobento/watertemplate-engine/badge.svg?branch=master)](https://coveralls.io/r/tiagobento/watertemplate-engine?branch=master)


Table of contents
--

- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Nested templates](#nested-templates)
- [Adding arguments](#adding-arguments)
- [Commands](#commands)
- [i18n](#i18n)
- [JAX-RS](#jax-rs)
- **[Try it yourself!](#try-it-yourself)**






## Quick start
##### Imagine a template:
```html
<h1>Months of ~year~</h1>
<ul>
    ~for month in months:
        <li>
            <span> ~month.lowerName~ </span>
            <span> with ~month.daysCount~ days </span>
        </li>
    :~
</ul>
```
Save it to `classpath:templates/en_US/months_grid.html`. Read [this](#where-to-store-your-template-files) to know why to save in this specific path.

##### Represent it in a Java class:
```java
class MonthsGrid extends Template {

    private static final Collection<Month> months = Arrays.asList(Month.values());

    MonthsGrid(final Year year) {
        add("year", year.toString());
        addCollection("months", months, (month, map) -> {
            map.add("lowerName", month.name().toLowerCase());
            map.add("daysCount", month.length(year.isLeap()) + "");
        });
    }

    @Override
    protected String getFilePath() {
        return "months_grid.html";
    }
}
```

##### Render it:
```java
public static void main(String[] args) {
    MonthsGrid monthsGrid = new MonthsGrid(Year.of(2015));
    System.out.println(monthsGrid.render());
}
```

##### See the result:
```html
<h1>Months of 2015</h1>
<ul>
    <li>
        <span> january </span>
        <span> with 31 days </span>
    </li>
    <li>
        <span> february </span>
        <span> with 28 days </span>
    </li>
    <li>
        <span> march </span>
        <span> with 31 days </span>
    </li>
    <li>
        <span> april </span>
        <span> with 30 days </span>
    </li>
    
    ... and so on
    
</ul>
```




## Configuration
Add the [maven dependency](http://mavenrepository.com/artifact/org.watertemplate/watertemplate-engine/1.1.0) to your project.
Since you've done that, extending `Template` gives you full power to build your templates. **Take a look at the [examples](watertemplate-example/src/main/java/org/watertemplate/example) and the source code!**

Read [this](#jax-rs) if you use RestEasy, Jersey or any JAX-RS implementation.

##### Where to store your template files?
Water will always search for your template files under `classpath:templates/[locale]/`, where `[locale]` is any locale of your choice. The default locale is `Locale.US`. It's easy to work this way when you use [Water i18n](watertemplate-i18n).

##### How to change the default locale?
Every `Template` has a method called `getDefaultLocale` which you can override. If you want to change the default locale for every template it's recommended that you create a class in the middle of `Template` and your `Templates` which overrides this method and propagates the change to its child classes.




 
## Adding arguments
Water works with a different approach to arguments. Unlike many other template engines, Water **uses no reflection at any time** and **doesn't make it possible to call functions within your template files**. Everything you add as an argument must have a key associated with it and can be formatted or manipulated through the mapping mechanism. 
There are five basic methods which let you add arguments:

```java
add("email", user.getEmail()); // takes a String
// Will match with ~email~
```

```java
add("user_is_popular", user.isPopular()); // takes a Boolean
// Will match with ~user_is_popular~
```

```java
addMappedObject("user", user, (userMap) -> { 
    userMap.add("email", user.getEmail());
}); 
// Will match with ~user.email~
```

```java
addCollection("users", users, (user, userMap) -> {
    userMap.add("email", user.getEmail());
});
// Will match with ~for user in users: ~user.email~ :~
```

```java
addLocaleSensitiveObject("now", new Date(), (now, locale) -> {
    return DateFormat.getDateInstance(DateFormat.FULL, locale).format(now); // returns a String
});
// Will match with ~now~
```


You can also nest `MappedObjects` and `LocaleSensitiveObjects` or add them inside a collection mapping:

```java
addCollection("users", users, (user, userMap) -> {
    userMap.addMappedObject("name", user.getName(), (name, nameMap) -> {
        nameMap.add("upper", name.toUpperCase());
    });
    userMap.addLocaleSensitiveObject("birth_date", user.getBirthDate(), (birthDate, locale) -> {
        return DateFormat.getDateInstance(DateFormat.FULL, locale).format(birthDate);
    });
});
// Will match with
//   ~for user in users: ~user.name~ was born in ~user.birth_date~ :~
// or also with
//   ~for user in users: ~user.name.upper~ was born in ~user.birth_date~ :~
```

It is only possible to add Strings and Booleans. Collections and MappedObjects are special types which should never be evaluated. **The `toString()` method is never implicitly called.**

## Nested templates
Water gives you the possibility to nest templates in many levels. Each `Template` can have one `MasterTemplate` and many `SubTemplates`. When creating a `Template`, you can override the `getMasterTemplate` and `getSubTemplates` methods to specify how is your tree going to be.

Also, each `Template` has one, and one only, template file associated with it. This 1 to 1 relationship ensures that
you cannot access other template files within your `Template` and you cannot access other `Templates` within your template files.

See an [example](watertemplate-example/src/main/java/org/watertemplate/example/nestedtemplates).





## Commands
Water provides **if** and **for** commands. 



- **_If:_** The if condition _must_ be a boolean. Null objects are not a valid condition.

- **_For:_** The for collection _must_ be added by the `addCollection` method. The else is triggered when the collection is empty or null.

##### Syntax
```html
~for user in users:
    
    <span> ~user.name~ </span>

    ~if user.is_already_followed:
        <input type="button" value="Unfollow"/>
    :else:
        <input type="button" value="Follow"/>
    :~
    
:else:
    <span> No users to display </span>
:~
```

## i18n
Water provides an i18n solution too. See the [i18n project](watertemplate-i18n) to know how to use it and why it works so good together with the engine.

## JAX-RS
If you want to provide your webpages as resources, JAX-RS is a good way to do that. Adding [this dependency](http://mavenrepository.com/artifact/org.watertemplate/watertemplate-jaxrs-binding/1.1.0) to your project lets you return a `Template` object directly. The locale will be injected during the rendering of each call, so your i18n is safe.

**Run an example** following the information below.

```java
@GET
@Path("/home")
public Template homePage() {
    return new HomePage();
}

@GET
@Path("/months/{year}")
public Template monthsGrid(@PathParam("year") Integer year) {
    return new MonthsGrid(Year.of(year));
}
```

## Try it yourself!

Go to the [examples project](watertemplate-example/) and follow the instructions.
