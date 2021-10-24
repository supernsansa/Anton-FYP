<?php
//This script retrieves the names of tags matching a search term
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

//Get word from app
$tag_name = $_POST["tagName"];
$myArray = array();
if ($result = $conn->query("SELECT DISTINCT TagName FROM tagdb WHERE TagName LIKE '%".$tag_name."%' 
	ORDER BY TagName ASC")) {

    while($row = $result->fetch_array(MYSQLI_ASSOC)) {
            $myArray[] = $row;
    }
    echo json_encode($myArray);
}
else {
	echo 'Some error has occured';
}

//$result->close();
$conn->close();
?>