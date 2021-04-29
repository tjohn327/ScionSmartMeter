package javadrkey

import (
	"context"
	"encoding/hex"
	"fmt"
	"github.com/scionproto/scion/go/lib/addr"
	"github.com/scionproto/scion/go/lib/sciond"
	"regexp"
	"time"

	"github.com/JordiSubira/drkeymockup/drkey"
	"github.com/JordiSubira/drkeymockup/mockupsciond"
)

var iaHostRegexp = regexp.MustCompile(`^((?:[-.\da-zA-Z]+)|(?:\d+)-[\d:A-Fa-f]+),(\[[^\]]+\]|[^\]:]+):(\d+)$`)
var sciondForClient = "127.0.0.1:30255"
var iaHostRegexpIAIndex = 1
var iaHostRegexpHostIndex = 2

func check(e error) {
	if e != nil {
		panic(fmt.Sprintf("Fatal error: %v", e))
	}
}

type Client struct {
	sciond sciond.Connector
}

func newClient(sciondPath string) Client {
	sciond, err := sciond.NewService(sciondPath).Connect(context.Background())
	check(err)
	return Client{
		sciond: sciond,
	}
}

func (c Client) hostKey(meta drkey.Lvl2Meta) drkey.Lvl2Key {
	ctx, cancelF := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancelF()
	var now = uint32(time.Now().Unix())
	// get L2 key: (slow path)
	key, err := mockupsciond.DRKeyGetLvl2Key(ctx, meta, now)
	check(err)
	return key
}

func thisClientAndMeta(dstIAStr, srcIAStr, dstHostStr, srcHostStr string) (Client, drkey.Lvl2Meta) {
	c := newClient(sciondForClient)

	dstIA, _ := addr.IAFromString(dstIAStr)
	srcIA, _ := addr.IAFromString(srcIAStr)
	dstHost := addr.HostFromIPStr(dstHostStr)
	srcHost := addr.HostFromIPStr(srcHostStr)
	meta := drkey.Lvl2Meta{
		KeyType:  drkey.Host2Host,
		Protocol: "piskes",
		SrcIA:    srcIA,
		DstIA:    dstIA,
		SrcHost:  srcHost,
		DstHost:  dstHost,
	}
	return c, meta
}

//export GetDRKey
func GetDRKey(serverAddr, clientAddr string) string {
	serverMatch := iaHostRegexp.FindStringSubmatch(serverAddr)
	if serverMatch == nil {
		fmt.Println("Destination Address not in the correct format")
		return ""
	}

	clientMatch := iaHostRegexp.FindStringSubmatch(clientAddr)
	if clientMatch == nil {
		fmt.Println("Source Address not in the correct format")
		return ""
	}

	client, metaClient := thisClientAndMeta(serverMatch[1], clientMatch[1], serverMatch[2], clientMatch[2])
	clientKey := client.hostKey(metaClient)
	return hex.EncodeToString(clientKey.Key)
}

