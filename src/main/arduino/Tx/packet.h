#ifndef __PACKET_H__
#define __PACKET_H__

#define CHANNEL 0x77
#define ADDRESS 0x77CECCCECC
#define MAGIC_ID 12

typedef struct payload_t
{
  uint8_t id; // Identification
  uint32_t date;
  uint16_t temp;
  uint16_t lum;
  uint16_t flow;
  uint16_t pH;
  uint16_t level;
} payload_t;

#endif

