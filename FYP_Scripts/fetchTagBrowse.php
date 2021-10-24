<?php
//This script retrieves the name of every tag in the database
//Server and DB connection values
$servername = "localhost";
$rootUser = "root";
$db = "bsl4kids";
$rootPassword = "";

//Create connection
$conn = new mysqli($servername, $rootUser, $rootPassword, $db);

//Check connection
if ($conn->connect_error)
	{
		die("Connection failed: " . $conn->connect_error);
	}

$myArray = array();
if ($result = $conn->query("SELECT DISTINCT TagName FROM tagdb ORDER BY TagName ASC")) {

    while($row = $result->fetch_array(MYSQLI_ASSOC)) {
            $myArray[] = $row;
    }
    echo json_encode($myArray);
}

$result->close();
$conn->close();
?>