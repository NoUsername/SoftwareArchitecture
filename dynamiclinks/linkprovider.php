<?php
  
  function endswith($string, $test) {
    $strlen = strlen($string);
    $testlen = strlen($test);
    if ($testlen > $strlen) return false;
    return substr_compare($string, $test, -$testlen) === 0;
  }
  
  $filetype = ".htm";
  
  function sendAsJson() {
    global $filetype;
    header('Content-type: application/json');
    if ($handle = opendir('./')) {
      // echo "Directory handle: $handle\n";
      // echo "Files:\n";
      
      echo '{"links":[';

      /* This is the correct way to loop over the directory. */
      $first = true;
      while (false !== ($file = readdir($handle))) {
        if (endswith($file, $filetype)) {
          if (!$first) {
            echo ',';
          } else {
            $first = false;
          }
          echo "\"$file\"";
        }
      }
      
      echo "]}";

      closedir($handle);
    }
  }
  
  function sendAsHtml() {
    global $filetype;
    if ($handle = opendir('./')) {
       
        /* This is the correct way to loop over the directory. */
        $first = true;
        $i = 1;
        while (false !== ($file = readdir($handle))) {
          if (endswith($file, $filetype)) {
            if (!$first) {
              echo ' - ';
            } else {
              $first = false;
            }
            echo "<a href=\"./$file\">Page $i</a> ";
            $i++;
          }
        }

        closedir($handle);
      }
  }

  sendAsHtml();

?>