HOMEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Home directory of the generator is $HOMEDIR"

#./sibgenerator 1 ${HOMEDIR}/outputDir/  ${HOMEDIR}/ -v

./sibgenerator 1 ${HOMEDIR}/outputDir/  ${HOMEDIR}/ -v -stream

#./sibgenerator 1 ${HOMEDIR}/outputDir/  ${HOMEDIR}/ -v -stream -isExp -calSP

#./sibgenerator 1 ${HOMEDIR}/outputDir/  ${HOMEDIR}/ -v -stream -isExp

#./sibgenerator 1 /export/scratch2/duc/work/SIB/workspace/SocialGraph/outputDir/ /export/scratch2/duc/work/SIB/workspace/SocialGraph/ -v -stream
