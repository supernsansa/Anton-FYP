<?php
//This script retrieves the name and like totals of every word in the database
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

//Get sort mode from app
$sort_mode = $_POST["sortMode"];

if($sort_mode === "A") {
	$sort_mode = "WordName ASC";
}
elseif($sort_mode === "Z") {
	$sort_mode = "WordName DESC";
}
elseif($sort_mode === "ML") {
	$sort_mode = "Likes DESC";
}
elseif($sort_mode === "FL") {
	$sort_mode = "Likes ASC";
}

$myArray = array();
if ($result = $conn->query("SELECT WordName, Likes FROM worddb ORDER BY " . $sort_mode)) {

    while($row = $result->fetch_array(MYSQLI_ASSOC)) {
            $myArray[] = $row;
    }
    echo json_encode($myArray);
}

$result->close();
$conn->close();
?>