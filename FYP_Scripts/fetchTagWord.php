<?php
//This script retrieves the names of all tags associated with a word 
//Likely to also change in future
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
$word_name = $_POST["wordName"];
$myArray = array();
if ($result = $conn->query("SELECT DISTINCT TagName FROM tagdb WHERE Word = '$word_name' ORDER BY TagName ASC")) {

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