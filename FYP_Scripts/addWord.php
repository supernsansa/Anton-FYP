<?php
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

//Get word and word description from app
$word_name = htmlspecialchars($_POST["wordName"]);
$word_desc = base64_encode($_POST["wordDesc"]);
//$user = $_POST["userName"];

//Query to worddb
$wordQuery = "INSERT INTO worddb (WordName, Definition) Values('$word_name','$word_desc')";
//Test if query is successful
if ($conn->query($wordQuery) == TRUE)
{
	/**
	//Query to videodb
	$videoQuery = "INSERT INTO videodb (Word, FileName, User) Values('$word_name','$word_name','$user')";
	//Test if query is successful
	if ($conn->query($videoQuery) == TRUE)
	{
		echo "Video entry created";
	}
	else
	{
		echo "Failure: For some reason";
	}
	*/
	$wordQuery = "SELECT WordID FROM worddb WHERE WordName = '$word_name'";
	$myArray = array();
	if ($result = $conn->query($wordQuery))
	{
		while($row = $result->fetch_array(MYSQLI_ASSOC)) {
            $myArray[] = $row;
		}
		echo json_encode($myArray);
	}
	else
	{
		echo "An error has occured2";
	}
}
else
{
  echo "An error has occured1";
  echo("Error description: " . $conn -> error);
}

$conn->close();
?>