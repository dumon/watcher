<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html lang="en">
<head>
        <meta charset="utf-8">
        <title>Network devices</title>
        <!-- Bootstrap core CSS -->
        <link href="webjars/bootstrap/4.3.1/css/bootstrap.css" rel="stylesheet">
    </head>

    <body>
        <div class="container" style="width: 300px;">
            <c:url value="/j_spring_security_check" var="loginUrl" />
            <form action="${loginUrl}" method="post">
                <h2 class="form-signin-heading">Please sign in</h2>
                <input type="text" class="form-control" name="j_username" placeholder="Email address" required value="user">
                <input type="password" class="form-control" name="j_password" placeholder="Password" required autofocus>
                <button class="btn btn-lg btn-primary btn-block" type="submit">Sign In</button>
            </form>
        </div>
    </body>
</html>