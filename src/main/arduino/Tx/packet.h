#ifndef __PACKET_H__
#define __PACKET_H__

#define CHANNEL 0x77
#define ADDRESS 0x77CECCCECC

enum class PacketID : uint8_t {
  Measure = 0,
  PHCalibration = 1
};

typedef struct measure_t {
  uint16_t temp;
  uint16_t lumP;
  uint16_t lumS;
  uint16_t flow;
  uint16_t pH;
  uint16_t level;
} measure_t;

typedef struct ph_calibration_t {
  uint16_t ph4;
  uint16_t ph7;
} ph_calibration_t;

typedef struct payload_t
{
  PacketID id; // Identification
  uint32_t date;
  union {
    measure_t measure;
    ph_calibration_t ph_calibration;
  };
} payload_t;

#endif

