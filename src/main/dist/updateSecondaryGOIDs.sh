# Replace GO secondary ids with GO primary ids, in GO annotations.

APPNAME="update-secondary-go-id-pipeline"
APPDIR=/home/rgddata/pipelines/$APPNAME
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`
EMAIL_LIST=mtutaj@mcw.edu
if [ "$SERVER" = "REED" ]; then
  EMAIL_LIST="rgd.devops@mcw.edu,jrsmith@mcw.edu"
fi

$APPDIR/_run.sh "$@" 2>&1 > $APPDIR/run.log

mailx -s "[$SERVER] Update Secondary GOIDs with Primary GOIDs in FULL_ANNOT OK" $EMAIL_LIST < $APPDIR/logs/summary.log
