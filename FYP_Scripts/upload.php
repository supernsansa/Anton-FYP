<?php
  
    $file_path = "Videos/";
    if(!is_dir($file_path)){
        //Directory does not exist, so lets create it.
        mkdir($file_path, 0755, true);
    }
    
    $file_path = $file_path . basename( $_FILES['uploaded_file']['name']);
	
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
        echo "success";
    } else{
        echo "fail";
    }
 ?>