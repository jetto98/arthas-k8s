wget $1 -O as-agent.tar.gz

tar zxvf ./as-agent.tar.gz

cd as-agent

chmod 777 asnode

./asnode -k $2 -n $3 --register-url $4 -p $5
