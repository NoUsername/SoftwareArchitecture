<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
               "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
 <title>Sensor Output</title>
</head>
<body>
<h1>Welcome to the HTML Sensor Output</h1>

<h3>please select a sensor:</h3>
<ul>
<?php

  function endswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr_compare($string, $test, -$testlen) === 0;
  }
  
  // very cheap, buggy, crappy, but works for our needs
  function isFolder($name) {
    if ($name == ".")
      return false;
    if ($name == "..")
      return false;  
    if (strpos($name, ".") == false)
      return true;
    return false;
  }
    
  $filetype = ".htm";
  
  function getFirstFileIn($dir) {
    global $filetype;
    if ($handle = opendir($dir)) {
      while (false !== ($file = readdir($handle))) {
          if (endswith($file, $filetype)) {
            return $file;
          }
        }
    }
    return false;
  }
  
  function printAsHtml() {
    global $filetype;
    if ($handle = opendir('./')) {
       
        /* This is the correct way to loop over the directory. */
        $first = true;
        $i = 1;
        while (false !== ($file = readdir($handle))) {
          if (isFolder($file)) {
            $firstFile = getFirstFileIn("./" . $file);
            if ($firstFile != false) {
              echo "<li><a href=\"./$file/$firstFile\">Sensor $i</a></li>";
              $i++;
            }
          }
        }

        closedir($handle);
      }
  }

  printAsHtml();

?>
</ul>
</body>
</html>