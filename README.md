Jiffy
=====

Jiffy is a quick and easy Java Web App Framework for scalable applications.

The Basics
=====

Jiffy is designed to allow you to quickly create a scalable and performant web application using Java.  Its inspiration comes after working with other frameworks
like Java's Play! framework, Ruby on Rails, and PHP's CodeIgniter.  It's design goals are simple:

* Allow intro-level and moderate-level Java programmers to use it
* Get an app up and running as quickly as possible
* Be as easy to learn as possible from a few examples
* Convention over configuration to the extreme
* Get an application that will scale to thousands of users out of the box

To be honest, I feel there is a HUGE discrepency between current Java web application frameworks like Play!, DropWizard, and vert.X and the skill level of a majority of Java programmers
(If you don't know the skill level of most Java programmers, just visit StackOverflow sometime).  For all these programmers, the popular frameworks are unusable.  The learning curve and
level of skill required to use these frameworks correctly are beyond the intro-level and moderate-level Java programmer.  While these frameworks are solving real-world problems, these
problems only exist in the most complex 5% of web applications.  For the other 95%, a simple and easy framework like Jiffy will give you everything you need with a much smaller learning curve.


####  Play! 2's complex example

    public static Promise<Result> index() {
      Promise<Integer> promiseOfInt = Promise.promise(
        new Function0<Integer>() {
          public Integer apply() {
            return intensiveComputation();
          }
        }
      );
      return promiseOfInt.map(
          new Function<Integer, Result>() {
            public Result apply(Integer i) {
              return ok("Got result: " + i);
            } 
          }
        );
    }
    
####  The same code in Jiffy

     public static ServiceResponse index(ServiceInput input) {
          int result = intensiveComputation();
          HttpResponse response = new HttpResponse();
          response.nextPage = "/index.jsp";
          return response;
     }

(Just to be clear, I LOVE Play!, especially version 1, but it's not a solution for everyone).

Building Blocks of Jiffy
-------
* Controllers that follow two conventions.  Choose the simple /controller/method format (RPC - Remote Procedure Call) like in CodeIgniter 
  or use a RESTful approach like in Rails.  Either way, no route files are necessary.
* The ability to deploy to one server or multiple servers without changing your code by abstracting out Sessions from a single JVM.
* An extremely powerful and scalable DB class, utilizing built in connection pools, and scalable to tens of thousands of concurrent users.
* Closely tied together Model and DB layers, allowing for extremely simple DB->Model coding.  No JPA or XML configuration files, just standard SQL code.
* Built in Security - protect every controller method and every JSP page with a simple Annotation.
* An abstraction of the Cache layer, utilizing a caching layer not tied to a single JVM.
* Built in web filters to cache and compress JS/CSS and images.
* Optimized for Apache Tomcat, and will run on any Java Servlet engine as a WAR file
* Works with MySQL, MariaDB and Postres databases
* Works with any memcached client (tested with Couchbase)

Database
=======

The database layer in Jiffy is designed to make it simple to code for, but also scalable to thousands of concurrent users. It was created to be much simpler to use than JPA for people
that are comfortable writing a little SQL code.

It utilizes connection pooling from Tomcat 7's DBPool class and database utility methods and abstractions from Apache's DBUtils class.  The end result is a DB utility class that handles
eveything you could want from your DB abstraction layer.

#### DB Examples

      // To get one User in the database
      DB.selectOne(UserData.class, "WHERE id=?", 1);

      // To get all the Users in the database
      DB.selectAll(UserList.class);
      
      // To count the users in the database
      DB.count(UserData.class);
      
      // To count the users with a distinct first name
      DB.countDistinct(UserData.class, "@firstName@");
      
      // It can also properly handle transactions
      try
      {
       DB.openTransaction();
       DB.commitTransaction();
      }
      catch (Exception ex)
      {
       DB.rollbackTransaction();
      }

Model
=====

The model layer is closely tied into the Database layer for a seamless integration just by using a few Annotations.

#### Model Examples

     @DBTable
     public class User
     {
      @DBColumn
      public String username;
      
      @DBColumn(name="family_name")
      public String lastName;
      
      @DBHasOne
      public Address address;
      
      @DBHasMany
      public PhoneNumber phoneNumbers;
     }
     

Model & DB Integration
=====

With the Model classes annotated properly, the DB abstraction layer can use introspection and reflection to create and map all the database fields into the Java objects.  Additionally,
the integration has been set up to create an ArrayList of objects when more than 1 object is returned from the SQL.

#### Model/DB Example

     // define the class to store many User objects
     public class UserList extends ArrayList<User>
     
     // Use the DB class to populate a List of admin Users
     UserList admins = DB.selectAll(UserList.class, "WHERE @role@=?, "admin");

Controllers
======
There are 2 styles you can use to route to your controllers, RPC-style or REST-style.  You can either use the PHP style controller/action to trigger the funcion controller.action() or you can use the RESTful
services like in Ruby on Rails.  Either way will work, it's up to you which one you prefer.


RPC-Style
------
You can define actions in your controller quickly using this style.  This always takes the form of "/rpc/controller/action", where the "rpc" prefix signifies how you want the domain
to be parsed by the server, RPC-Style as opposed to REST-Style.

In your JSP pages, you should refer to the controllers and the actions in an "underscore" style.  The controllers should then be named in a "camelCase" style.  This is the common setup
for all Java/PHP/Ruby frameworks.  The conversion happens automatically on the server.

##### Example of RPC-Style Controllers


       <!-- On the HTML page -->
       <form method="post" action="/rpc/user_session/login">
       
       // Will trigger this function 
       public class UserSessionController
       {
          public static ServiceResponse login(ServiceRequest input){}
       }

REST-Style
-----
The REST style implementation of the controllers has been copied directly from Rails.  The 6 possible REST actions are mapped directly to 6 method names in the controller.

     GET => index()
     POST => create()
     GET w/ ID => show()
     DELETE w/ ID => destroy()
     POST w/ ID => update()
     PUT w/ ID => update()
 
##### Example of REST-Style Controllers


       <!-- On the HTML page -->
       <form method="post" action="/rest/user/1">
       
       // Will trigger this function 
       public class UserController
       {
          public static ServiceResponse update(ServiceRequest input){}
       }
       
Parameters
------

The Objects ServiceRequest and ServiceResponse contain all the request attributes you need to parse the request and the response contains all the attributes needed for a response.

    String username = input.req.getParameter("username");
    String password = input.req.getParameter("password");
    
Responses
-------
Then, at the end of your method, you can return the proper response type, either a HttpResponse, JsonResponse, ErrorResponse, or NoResponse. The server will handle
everything for you from that point.

    HttpResponse response = new HttpResponse();
    response.nextPage = "/index.jsp";
	return response;

    JsonResponse response = new JsonResponse();
    response.text = new JSONObject(myObject);
    return response;

Security on Controllers and JSP's
-------
Security and Authorization is built into Jiffy.  I always felt that every framework left this off the list of their features - every application will ultimately have a user login feature, so
why not include it in the framework itself.

Every Controller and JSP has the ability to add Access control to it for certain users.  This can be done in the Controller by simply adding an Annotation to the method with an access level
for the function

    // only the ADMIN can access this function - all others will get a 500 error
    @Service(access=Roles.ADMIN)
	public static ServiceResponse index(ServiceRequest input) throws Exception
	
	// or in a JSP file
    Security.validateAccess(request, Roles.ADMIN);

Jiffy Configuration Files
====
I've always hated in frameworks that they don't include a simple way to provide configuration settings, and a simple way to access them.  Rails is a good example of the right way to do it, which
uses global variables to provide access to configuration variables.  Jiffy tries to make this even easier.

Any configuration you place into one of the properties files will be accessible anywhere in the application.

Jiffy loads up 3 configuration files, 1)  jiffy.properties  2) environment.properties 3) ENVIRONMENT.properties with each property file overriding the settings already loaded.  This lets you nest
configurations and specify settings specific to only one deployment.

### Config Example

      // In jiffy.properties
      dbPassword = ABCDEF123
      
      // In the file named environment.properties
      environment = development
      
      // In the file named development.properties
      dbPassword =
      
      ///////
      // If the environment was set to production instead
      ///////
      
      // In the file named production.properties
      dbPassword = VERY_STRONG_PASSWORD
      
The result of these configurations lets you set global variables, and environment-specific variables.  This lets you set variables for the application, while each developer can also specify
their own variables for their own environment.  

NOTE - the file "local.properties" is included in the .gitignore file, so feel free to store local passwords and api keys there.
   

Sessions
=====
Just as the Cache was abstracted away between one server deployments and multiple server deployments, the Session have been abstracted away as well.  You can now feel comfortable using Sessions
as they were intended to be used on a single JVM deployment and not worry about the underlying issues when moving to a multi-server deployment.  When the app is running on 1 JVM, Jiffy
will utilize the standard HttpSession built into the JVM.  When the app is running on multiple JVM's, Jiffy will store active sessions in the DB, with an eviction thread deleting invalid sessions,
and the Cache storing Session attributes.  In this way, you can write your multi-server application as if it was running on 1 server.

    Sessions.addSession()
    Sessions.deleteSession()
    Sessions.set("1", "Apple");
    String apple = Sessions.get("1");

NOTE - You should NEVER use the HttpSession object directly when working with Jiffy, as this will restrict your application to running on one JVM.


Cache
=====
There is an easy to use Cache class that lets you place items in cache for fast read/write access.  The Cache class serves as an abstraction to the underlying cache implementation.  The underlying
cache implementation is dependent on the setup chosen - single server deployments will utilize the JVM cache, and store items in a ConcurrentHashMap for optimal speed, while a multi-server
deployment will store items in Couchbase, which wraps around a memcached solution.

In beauty of the abstraction is that you can write your Java application agnostic to the future deployment and whether the app will be running on 1 JVM or many JVM's.  Experienced Java devs
know this has not always been easy.

     Cache.set("1", "Apple");
     String apple = Cache.get("1");


Other Features
========

* All JS/CSS/Images are cached and compressed automatically, for quicker page load speeds on the client
* Flash - store messages and other objects going back to the client on the next page in the Flash object on the server so they are available on the JSP page
* Many Util classes for dealing with common Java issues - LogUtil, MathUtil, NumberUtil, PasswordUtil, PusherUtil, TimeUtil, TwilioUtil, ZipUtil
* Includes the Parallel utility, to run tasks in Parallel without using Java 8


In Conclusion
======
It's been said that a good web app framework needs sessions, authentication, routing, MVC, caching, mobile, templating, ORM, and testability.  Jiffy has all of these things
right out of the box.  It's the smallest learning curve of any Java web app framework out there.  It might not do everything frameworks like Play!, vert.X and DropWizard do, but it does
everything you'd need 95% of the time, and will get you up running 50% faster.


Deployment
======
Download all the files in the lib directory of the project, which includes the jiffy.jar file as well as all its dependencies.  Place all the JAR files in your web application's
WEB-INF/lib folder.  Place all the PROPERTIES and XML files in your web application's WEB-INF/classes folder.  Use the code in the examples folder as a guide to get started.

The db folder of the project contains an SQL script to set up the database table correctly for the UserSession table.


TODO
=====
* Find a better JSON->Java implementation - maybe Jackson?
* Replace the basic ConcurrentHashMap cache with a better more tuned cache from Guava
* MongoDB support probably needs to be added
* Maven support
