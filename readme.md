Getting Started
---------------
1. Create a Firebase account and import the baseline data.
2. Create a Firebase bucket to hold the movie clips.
3. Set the URLs in your gradle project properties file (gradle.properties). You can use the gradle.properties.example file for an example.
4. Setup Google security from the Firebase console.
5. Download the google-servcies.json file from firebase and put in the app directory.

Rules
-----
1. Plays have two parts only
1. When you browse movie scripts and tap add, you create a clone of the 
Movie and become its owner
1. If you leave the RecorderActivity before recording all the lines:
  a. the next time you enter the RecorderActivity you are taken to the last line not recorded
  b. your progress is saved
1. When you record all the lines for a part:
  a. you can now share it, as long as you haven't recorded the other part yet.
  b. you can no longer record lines for the part
1. If you have recorded a part, the button to record that part becomes disabled.
1. You can only share movies that are in the RECORDED or SHARED states.

Sharing
-------
1. When you save your movie, you also create a Share. The Share ID is saved with
the movie. 
1. The Share contains: 
  a. a share ID
  b. movie template ID
  c. a path back to the movie (moviePath)
  d. the owner's part (blank until you record the first part)
  e. the owner's clip collection with key of lineID and value of clipStorageBucketPath
  f. the contributor's part (blank until you share)
  g. a contributors collection keyed by contributor UID that contains
    i.  the contributor's name (if possible)
    ii. a partClips collection with key of lineID and value of clipStorageBucketPath
1. When (before) you share your movie:
 a. Your clips are uploaded 
 b. The contributorsPart is added to the Share object
 c. The owner is no longer able to record the other part.
1. When you record your first part, that part is saved in the share as the 
ownersPart. The contributorsPart becomes the other one.
1. The clips storage bucket just contains all the clips.
1. Once shared, the owner listens for share contributions. When one is detetected, the original
movie is cloned and the clips are downloaed. Then the cloned movie's state is changed to DOWNLOADED
at this point, the movie can be viewed in the MovieActvity.
1. From the contributors perspective, once their part has been recorded, the owner clips are downloaded and
the movie can be viewed in the MovieActivity.
