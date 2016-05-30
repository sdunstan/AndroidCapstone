TODO and Rules
==============

1. Add all image sizes for the banner. Figure out if it should be in mipmap 
or whatever.
1. Program rules
1. Add share button to PlayViewActivity
1. Add play button to Script Card
1. Make controls visible after last line in a part is read.
1. Add help snackbars & toasts
1. Simplify state machine
1. MainActivity layout is fckd u
1. Implement rules

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
1. If you tap a part to record it and you have already recorded the other part, 
you are warned that this will make the play unsharable.
1. You can only share movies that are in the RECORDED or SHARED states.
1. We will need some cap to the number of times a shared movie can be cloned. (5?)

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

Merging
-------
#### Owner's perspective single user
1. You choose to record all the parts yourself, you can't share and your clips
all stay local.
1. After you record the second part, all the local clips are merged, the 
resulting movie is saved locally, and the clips are deleted. Final state is
SINGLE USER MERGED.

#### Owner's perspective multi-user
1. Listens on contributors collection. Every time it detects a contributor has
been added to the contributor's collection, it creates a shallow copy of the
movie in the owner's movie collection. Movie state will be CONTRIBUTED.
1. Clips are downloaded from GCS
1. Movie is merged, state is transitioned to MERGED, downloaded clips are deleted.

Final state is that the owner has a merged copy of the movie in their library for every contributor.

#### Contributor's pserspective
1. Clicks on deep link. Play view activity queries the share by share ID and then queries the movie
by the moviePath in the Share object.
1. Only the part available is available to record (button active)
1. When, the button is pressed:
  a. A copy of the movie is added to the user's movies collection with state CONTRIBUTE
  b. Clips begin to download from the owner
1. When all the lines are recorded, the state becomes CONTRIBUTED. If all owner lines have been
downloaded, merge begins.
1. When all owner clips have been downloaded, check to see if the lines have been recorded. If so,
begin merge.
1. When merge happens, set state to MERGED, begin upload of clips to GCS.
1. When all clips are uploaded to GCS add self to contributors collection of Share.

Final state is contributor has a merged copy of the movie in their library.

