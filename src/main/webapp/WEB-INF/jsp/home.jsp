<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib prefix="dev" uri="/WEB-INF/tag/device.tld" %>
<%@ page errorPage = "error.jsp" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Network devices</title>
        <!-- Bootstrap core CSS -->
        <link href="webjars/bootstrap/4.3.1/css/bootstrap.css" rel="stylesheet">
        <!-- JQuery core JS -->
        <script src="webjars/jquery/3.4.1/jquery.min.js"></script>
    </head>
    <script type="text/javascript">
        function updateDevice(id, name, ip, active, lastActiveTime) {
            console.log("update device: id '" + id + "', new name '" + name + "'");
            $.ajax({
                   url: '/devices/' + id,
                   type: 'PUT',
                   data: '{"ipAddress": "' + ip + '", "name": "'+ name + '", "active": "' + active + '", "lastActiveTime": "' + lastActiveTime + '"}',
                   contentType: 'application/json'
               })
            .done(function() {
                location.reload()
            })
            .fail(function() {
                console.log("error");
            });
        }

        function keyPress(event, btn) {
            if (event.keyCode == 13) {
                btn.onclick();
            }
        }
    </script>

    <body>
        <div class="container">
            <sec:authorize access="!isAuthenticated()" method="GET" url="/login">
                <% response.sendRedirect("login"); %>
            </sec:authorize>

            <sec:authorize access="isAuthenticated()">
                <div class="starter-template">
                    <h1>Network Devices</h1>
                    <hr style="margin-top: 0px;border-top: 1px solid #337ab7">
                </div>

                <c:choose>
                    <c:when test="${empty devices}">
                            No devices found...
                    </c:when>
                    <c:otherwise>
                        <table class="table">
                            <thead class="thead-dark">
                            <tr>
                                <th>Name</th>
                                <th>IP</th>
                                <th>MAC</th>
                                <th>Status</th>
                                <th>LastActive</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${devices}" var="device">
                                <tr>
                                    <td>
                                        <div onclick="this.parentElement.children[1].style.display = 'flex';this.style.display = 'none'"><jsp:text>${empty device.name ? 'Not identified' : device.name}</jsp:text></div>
                                        <div class="input-group" style="display: none">
                                            <input type="text" class="form-control" placeholder="<jsp:text>${device.name}</jsp:text>" aria-label="device name" aria-describedby="basic-addon2" onkeypress="keyPress(event, this.nextSibling.nextSibling)">
                                            <button type="button" id="${device.macAddress}"
                                                    onclick="updateDevice(this.id, this.previousSibling.previousSibling.value, '${device.ipAddress}', '${device.active}', '${device.lastActiveTime}')"
                                                    class="btn btn-danger" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                                Action
                                            </button>
                                        </div>
                                    </td>
                                    <td><jsp:text>${device.ipAddress}</jsp:text></td>
                                    <td><dev:mac>${device.macAddress}</dev:mac></td>
                                    <td><jsp:text>${device.active ? 'Active' : 'Not Active'}</jsp:text></td>
                                    <td><fmt:formatDate value="${device.lastActiveTime}" pattern="yyyy-MM-dd HH:mm" /></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </c:otherwise>
                </c:choose>

                <hr style="margin-top: 0px;border-top: 1px solid #337ab7">
                <p>Your login: <sec:authentication property="principal.username" /></p>
                <p><a class="btn btn-lg btn-danger" href="<c:url value="/logout" />" role="button">Logout</a></p>

            </sec:authorize>
        </div>
    </body>
</html>