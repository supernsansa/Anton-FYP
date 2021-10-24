<?php
//This script fetches all word information from an inputted name
//Videos are retrieved in another script
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
$word_name = $_POST["wordName"]; //"Fish"; Debug
$myArray = array();
if ($result = $conn->query("SELECT WordID, WordName, FROM_BASE64(Definition) as Definition, DateAdded, Likes, ImageFilename FROM worddb where WordName = '$word_name'")) {

    while($row = $result->fetch_array(MYSQLI_ASSOC)) {
            $myArray[] = $row;
    }
    echo json_encode($myArray);
}

$result->close();
$conn->close();
?>