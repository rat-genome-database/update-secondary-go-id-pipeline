# Replace GO secondary ids with GO primary ids, in GO annotations.

APPNAME=updateSecondaryGOID
APPDIR=/home/rgddata/pipelines/$APPNAME
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`
EMAIL_LIST=mtutaj@mcw.edu
if [ "$SERVER" = "REED" ]; then
  EMAIL_LIST="rgd.developers@mcw.edu,jrsmith@mcw.edu"
fi

$APPDIR/_run.sh "$@" 2>&1 > $APPDIR/logs/status.log

mailx -s "[$SERVER] Update Secondary GOIDs with Primary GOIDs in FULL_ANNOT table" $EMAIL_LIST < $APPDIR/logs/status.log