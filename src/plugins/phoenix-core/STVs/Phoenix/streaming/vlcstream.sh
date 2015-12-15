#!/bin/bash

# This Environment is set when the script is called
# PMS_BASE_URL
# - The Server's URL for this current working directory 
#
# PMS_CLIENT_ID
# - Unique ID for the requesting client
#
# PMS_MEDIA_ID
# - The SageTV MediaFileID for the requested stream
#
# PMS_PLAYLIST_FILE
# - If you are creating a .m3u8 playlist file, then write to this file
#
# PMS_CLIENT_NETWORK
# - will be 'wifi' or 'mobile' letting you know the type of network on which the client is currently using
#
# PMS_PROFILE_NAME
# - client will pass one of 'low', 'normal', or 'hd'.  You can optionally use this to create a different
#   stream based on the client's network and requested profile
#
# PMS_CLIENT_SCREEN
# - WxH containing the client's screen Width and Height.

# PMS_CONTROL_FILE
# - a .properties file where you can optionally write control information.  The only supported property for now
#   is 'media_url' which would contain a new URL that you are sending back to the client for streaming.
#   the control file is only used in advanced cases where you are wanting to create a completely new type of
#   of stream reply for the client.  The only reason to use this, is that your are not using the PMS_PLAYLIST_FILE
#   and your process may forward the user another site for playback. 
#

# The parameters to the script are either
# - the word DESTROY meaning to shutdown/cancel any existing streaming request for this client
# - or $1 is the full path to the file to be streamed

# The Script is executed in the expected output directory for the stream request.  ie, the server creates a client
# specific area for the files, then calls this script.  This script needs to create a process that will start
# creating the streaming files.

# check if we need to destroy any running processes
if [ "$1" = "DESTROY" ] ; then
	ps -ef | grep vlc | grep ${PMS_CLIENT_ID} | awk '{print $2}' | xargs kill -9
	exit 0;
fi

# create hls stream
/usr/bin/cvlc -I dummy --mms-caching 0 "$1" vlc://quit --sout="#transcode{width=320,height=240,fps=25,vcodec=h264,vb=256,venc=x264{aud,profile=baseline,level=30,keyint=30,ref=1},acodec=mp3,ab=96}:std{access=livehttp{seglen=5,delsegs=false,numsegs=0,index=${PMS_PLAYLIST_FILE},index-url=${PMS_BASE_URL}${PMS_MEDIA_ID}-########.ts},mux=ts{use-key-frames},dst=${PWD}/${PMS_MEDIA_ID}-########.ts}"
